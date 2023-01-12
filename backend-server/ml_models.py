import feature_extraction 
from transformers import Wav2Vec2ForCTC, Wav2Vec2Tokenizer
from transformers import pipeline
import librosa
import torch


summarizer = None
tokenizer = None
model = None

def init_models():
    global summarizer 
    global tokenizer
    global model
    summarizer = pipeline("summarization", model="google/pegasus-xsum")
    tokenizer = Wav2Vec2Tokenizer.from_pretrained("facebook/wav2vec2-base-960h")
    model = Wav2Vec2ForCTC.from_pretrained("facebook/wav2vec2-base-960h")


def return_keywords(text):
    tr4w = feature_extraction.TextRank4Keyword()
    tr4w.analyze(text, candidate_pos = ['NOUN', 'PROPN'], window_size=4, lower=False)
    return tr4w.get_keywords(10)


def condensed_text(path):
    speech, rate = librosa.load(path, sr = 16000)
    final_text = ""

    splits = librosa.effects.split(speech, top_db=10)
    for beg, end in splits:
        y = speech[beg: end]
        if len(y) < 10:
            continue
        input_values = tokenizer(y, return_tensors = 'pt').input_values
        logits = model(input_values).logits
        predicted_ids = torch.argmax(logits, dim =-1)
        transcriptions = tokenizer.decode(predicted_ids[0])
        final_text += " " + transcriptions
    print("Converted text is", final_text.lower())
    return summarizer(final_text.lower(), do_sample=False)[0]['summary_text']