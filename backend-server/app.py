import json
from flask import Flask

app = Flask(__name__)

if __name__ == '__main__':
    CONFIG = None
    with open("./config.json", "rb") as f:
        CONFIG = json.load(f)

    # Initialize stuff here

    app.run(
        host=CONFIG["APPLICATION"]["HOST"],
        port=CONFIG["APPLICATION"]["PORT"], 
        debug=CONFIG["APPLICATION"]["DEBUG"]
    )
