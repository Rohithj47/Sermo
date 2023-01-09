import json
import os
from flask import Flask, request
from pymongo import MongoClient
from pydub import AudioSegment
from os import environ 
import feature_extraction 
from ml_models import init_models, return_keywords, condensed_text

from transformers import Wav2Vec2ForCTC, Wav2Vec2Tokenizer
from transformers import pipeline

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



def get_database():
   CONNECTION_STRING = environ.get('MONGOURI')
   client = MongoClient(CONNECTION_STRING)
   return client['Sermo']

if __name__ == '__main__':
    CONFIG = None
    with open("./config_sample.json", "rb") as f:
        CONFIG = json.load(f)
    CONFIG["DEBUG"] = True

    init_models()   
    

    app.run(
        host=CONFIG["APPLICATION"]["HOST"],
        port=CONFIG["APPLICATION"]["PORT"], 
        debug=CONFIG["APPLICATION"]["DEBUG"]
    )
