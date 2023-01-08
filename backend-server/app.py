import json
import os
from flask import Flask, request
from transformers import pipeline
from pydub import AudioSegment
import librosa
import torch
from transformers import Wav2Vec2ForCTC, Wav2Vec2Tokenizer

app = Flask(__name__)

file_names = ["file1", "file2", "file3"]

@app.route('/hi')
def hello():
    return "Hi!"

@app.route('/summarize', methods = ['POST'])
def summarize():
    if request.method == "POST":
        if request.files:
            files = []
            m4a_paths = []
            wav_paths = []
            final_patient_record = ""
            for i in range(len(file_names)):
                files.append(request.files[file_names[i]])
            for i in range(len(files)):
                m4a_paths.append(os.path.join('static/m4a_recordings', files[i].filename))
                wav_paths.append(os.path.join('static/wav_files', files[i].filename))
                files[i].save(m4a_paths[i])
                track = AudioSegment.from_file(m4a_paths[i],  format= 'm4a')
                track.export(wav_paths[i], format='wav')
                final_patient_record += condensed_text(wav_paths[i])

        return final_patient_record             


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
    summarizer = pipeline("summarization", model="google/pegasus-xsum")
    #load pre-trained model and tokenizer
    tokenizer = Wav2Vec2Tokenizer.from_pretrained("facebook/wav2vec2-base-960h")
    model = Wav2Vec2ForCTC.from_pretrained("facebook/wav2vec2-base-960h")

    app.run(
        host=CONFIG["APPLICATION"]["HOST"],
        port=CONFIG["APPLICATION"]["PORT"], 
        debug=CONFIG["APPLICATION"]["DEBUG"]
    )
