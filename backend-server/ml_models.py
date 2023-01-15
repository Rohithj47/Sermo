import feature_extraction 
import requests


summarizer = None
tokenizer = None
model = None

summarizer_url = None
speech2text_url = None 
bearer_token = "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"

def init_models():
    global summarizer 
    global tokenizer
    global model
    global summarizer_url
    global speech2text_url

    summarizer_url = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn"
    speech2text_url = "https://api-inference.huggingface.co/models/facebook/wav2vec2-base-960h"



def return_keywords(text):
    tr4w = feature_extraction.TextRank4Keyword()
    tr4w.analyze(text, candidate_pos = ['NOUN', 'PROPN'], window_size=4, lower=False)
    return tr4w.get_keywords(10)


def condensed_text(path):
    sound_file = open(path, 'rb')
    token = "Bearer " + bearer_token
    headers = {"Authorization" : token}
    s2t_response = requests.post(speech2text_url, sound_file, headers=headers)
    if s2t_response.status_code != 200:
        print("Error in speech model", s2t_response.text)
        return ''
    generated_text = s2t_response.json()["text"].lower()
    summarizer_res = requests.post(summarizer_url, generated_text, headers=headers)
    if summarizer_res.status_code != 200:
        print("Error is summarizer model", summarizer_res.text)
        return ''
    return summarizer_res.json()[0]["summary_text"]
    



