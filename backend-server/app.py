import json
import os
from flask import Flask, request
from pymongo import MongoClient
from pydub import AudioSegment
from os import environ 
from ml_models import init_models, return_keywords, condensed_text
import json

app = Flask(__name__)
file_names = ["patient_recording","surgery_conversation", "doctor_review"]

@app.route('/hi')
def hello():
    return "Hi!"

@app.route('/summarize/', methods = ['POST'])
def summarize():
    if request.method == "POST":
        if request.files:
            files = []
            m4a_paths = []
            wav_paths = []
            final_patient_record = ""
            patient_reports = {}
            name = request.form.get('fullname')
            age = request.form.get('age')
            gender = request.form.get('gender').lower()
            for i in range(len(file_names)):
                files.append(request.files[file_names[i]])
            for i in range(len(files)):
                m4a_paths.append(os.path.join('static/m4a_recordings', files[i].filename))
                wav_paths.append(os.path.join('static/wav_files', files[i].filename))
                files[i].save(m4a_paths[i])
                track = AudioSegment.from_file(m4a_paths[i],  format= 'm4a')
                track.export(wav_paths[i], format='wav')
                patient_reports[file_names[i]] = condensed_text(wav_paths[i])
                final_patient_record += patient_reports[file_names[i]]

        print("final Text is", final_patient_record)
        keywords = return_keywords(final_patient_record)
        keywords.append(gender)
        keywords.append(age)
        print("Keywords from test is", keywords)

        delete_recordings(m4a_paths, wav_paths)

        # db = get_database()
        client = MongoClient("mongodb+srv://m001-student:m001-mongodb-basics@cluster0.gbvuu.mongodb.net/Sermo?retryWrites=true&w=majority")
        db = client['Sermo'].Sermo

        x = db.insert_one({ "full_name": name, "age": age, "gender": gender, "Keywords": [], "Report" : final_patient_record, "consultation_summary": patient_reports["patient_recording"], "operation_summary": patient_reports["surgery_conversation"], "review_summary": patient_reports["doctor_review"]})
        db.update_one({'_id': x.inserted_id}, {'$push' : {'Keywords' : {'$each' : keywords}}})

        return final_patient_record             


@app.route('/search/', methods = ['POST'])
def search_documents():
    if request.method == 'POST':
        val = request.json
        tags = val['tags']
        client = MongoClient("mongodb+srv://m001-student:m001-mongodb-basics@cluster0.gbvuu.mongodb.net/Sermo?retryWrites=true&w=majority")
        db = client['Sermo'].Sermo
        # db = get_database()
        docs = list(db.find({}))
        res = []
        for doc in docs:
            matched = 0
            for tag in tags:
                if tag.lower() in doc['Keywords']:
                    matched += 1
            doc['Matched'] = matched
            if matched != 0:
                res.append(doc)

        res.sort(key= lambda doc: doc['Matched'], reverse= True)
        return prepare_payload(res)
        

def prepare_payload(docs):
    payload = {}
    ret = []
    for doc in docs:
        doc.pop('Keywords')
        doc.pop('Report')
        doc.pop('Matched')
        doc['id'] = str(doc['_id'])
        doc.pop('_id')
    payload["reports"] = docs
    ret = []
    ret.append(payload)
    return ret

def get_database():
    #Returns the database connection to MongoURL
   CONNECTION_STRING = environ.get('MONGOURI')
   client = MongoClient(CONNECTION_STRING)
   return client['Sermo'].Sermo

def delete_recordings(m4a_paths, wav_paths):
    #Deletes the static file paths => No reason to persist.
    for i in range(len(m4a_paths)):
        os.remove(m4a_paths[i])
        os.remove(wav_paths[i])

if __name__ == '__main__':
    CONFIG = None
    with open("./config.json", "rb") as f:
        CONFIG = json.load(f)
    CONFIG["DEBUG"] = True

    init_models()   
  

    app.run(
        host=CONFIG["APPLICATION"]["HOST"],
        port=CONFIG["APPLICATION"]["PORT"], 
        debug=CONFIG["APPLICATION"]["DEBUG"]
    )
