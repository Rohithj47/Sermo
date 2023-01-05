import json
import os
from flask import Flask, request
from transformers import pipeline
from pydub import AudioSegment
import librosa
import torch
from transformers import Wav2Vec2ForCTC, Wav2Vec2Tokenizer

app = Flask(__name__)

@app.route('/hi')
def hello():
    return "Hi!"

@app.route('/summarize', methods = ['POST'])
def summarize():
    if request.method == "POST":
        if request.files:
            file = request.files["files"]
            m4a_path = os.path.join('static/m4a_recordings', file.filename)
            wav_file = os.path.join('static/wav_files', file.filename)
            file.save(m4a_path)
            track = AudioSegment.from_file(m4a_path,  format= 'm4a')
            file_handle = track.export(wav_file, format='wav')

            return condensed_text(wav_file)


def condensed_text(path):
    speech, rate = librosa.load(path, sr = 16000)
    input_values = tokenizer(speech, return_tensors = 'pt').input_values
    logits = model(input_values).logits
    predicted_ids = torch.argmax(logits, dim =-1)
    transcriptions = tokenizer.decode(predicted_ids[0])
    return summarizer(transcriptions, do_sample=False)

if __name__ == '__main__':
    CONFIG = None
    with open("./config_sample.json", "rb") as f:
        CONFIG = json.load(f)
    CONFIG["DEBUG"] = True

    # Initialize stuff here
    summarizer = pipeline("summarization", model="facebook/bart-large-cnn")
    #load pre-trained model and tokenizer
    tokenizer = Wav2Vec2Tokenizer.from_pretrained("facebook/wav2vec2-base-960h")
    model = Wav2Vec2ForCTC.from_pretrained("facebook/wav2vec2-base-960h")

    app.run(
        host=CONFIG["APPLICATION"]["HOST"],
        port=CONFIG["APPLICATION"]["PORT"], 
        debug=CONFIG["APPLICATION"]["DEBUG"]
    )
