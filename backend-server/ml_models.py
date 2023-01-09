import feature_extraction 
from transformers import Wav2Vec2ForCTC, Wav2Vec2Tokenizer
from transformers import pipeline
import librosa
import torch


summarizer = ""
tokenizer = ""
model = ""

def init_models():
    summarizer = pipeline("summarization", model="google/pegasus-xsum")
    tokenizer = Wav2Vec2Tokenizer.from_pretrained("facebook/wav2vec2-base-960h")
    model = Wav2Vec2ForCTC.from_pretrained("facebook/wav2vec2-base-960h")


def return_keywords(text):
    tr4w = feature_extraction.TextRank4Keyword()
    tr4w.analyze(text, candidate_pos = ['NOUN', 'PROPN'], window_size=4, lower=False)
    return tr4w.get_keywords(10)


def condensed_text(path):
    speech, rate = librosa.load(path, sr = 16000)
    input_values = tokenizer(speech, return_tensors = 'pt').input_values
    logits = model(input_values).logits
    predicted_ids = torch.argmax(logits, dim =-1)
    transcriptions = tokenizer.decode(predicted_ids[0])
    return summarizer(transcriptions, do_sample=False)