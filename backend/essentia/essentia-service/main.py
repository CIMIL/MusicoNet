import json
import statistics
import jwt
from jwt import PyJWKClient
import py_eureka_client.eureka_client as eureka_client
from flask import Flask, request, jsonify, Response
from kafka import KafkaProducer
from pprint import pprint
from flask_restful import Api

producer = KafkaProducer(bootstrap_servers='localhost:19092')
from tempfile import TemporaryDirectory

import essentia.standard as es
from essentia.standard import MusicExtractor , TensorflowPredictMusiCNN

# prefix = "/mnt/c/Users/jacot/Music/"

ESSENTIA_SERVER_PORT = 9090

eureka_client.init(
    eureka_server="http://user:password@localhost:8761",
    app_name="ESSENTIA-SERVICE",
    instance_port=ESSENTIA_SERVER_PORT
)
app = Flask(__name__) 
api = Api(app)

to_skip = ["tonal.chords_histogram"]
metadata = {}
with open('msd-musicnn-1.json', 'r') as json_file:
    metadata = json.load(json_file)


def parse_auth_token(request):
    jwks_client = PyJWKClient('http://204.216.223.231:8082/realms/musico-realm/protocol/openid-connect/certs')
    data = request.headers['Authorization']
    token = str.replace(str(data), 'Bearer ', '')

    print("Token:", token)
    signing_key = jwks_client.get_signing_key_from_jwt(token)
    singing_algos = ['RS256']
    data = jwt.decode(token, signing_key.key, algorithms=singing_algos, audience='account')
    pprint(data)
    return data


def genreMoodPrediction(file_path):
    audio = es.MonoLoader(filename=file_path, sampleRate=16000)()
    # Extract the features
    activation = TensorflowPredictMusiCNN(graphFilename="./msd-musicnn-1.pb",batchSize=-1)(audio)
    pprint(activation)
    genres = {}
    mood = {}
    for i, key in enumerate((metadata['classes'])):
        if "happy" in key or "sad" in key or "chill" in key:
            mood[key] = statistics.mean(activation.T[i])
        else:
            genres[key] = statistics.mean(activation.T[i])
    
    #Select the 3 most probable classes
    genres = dict(sorted(genres.items(), key=lambda item: item[1], reverse=True)[:3])
    return genres, mood

def featurePrediction(audio_path):
    features , f_frames = es.MusicExtractor(
        rhythmStats=[],
        tonalStats=[],
    )(audio_path)
                                            
    bpm = features['rhythm.bpm']
    key = features['tonal.chords_key']
    scale = features['tonal.chords_scale']
    danceability = features['rhythm.danceability']
    return bpm, key, scale, danceability


@app.route('/audio/analysis', methods=['GET', 'POST'])
def audio_analysis(file=None):
    username = parse_auth_token(request)['sub']
    print("audio_analysis():")
    # Get the file from the request
    data = {}
    if request.method == 'POST':
        if 'file' not in request.files:
            return Response("No file part", status=400, mimetype='application/json')
        file = request.files['file']
        if file.filename == '':
            return Response("No selected file", status=400, mimetype='application/json')
        
        with TemporaryDirectory() as tmpdirname:
            file_path = tmpdirname + '/' + file.filename
            file.save(file_path)
            print("file_path:", file_path)
            # Load the audio file
            genres, mood = genreMoodPrediction(file_path)
            bpm, key, scale, danceability = featurePrediction(file_path)
            
            data["genres"] = [*genres.keys()]
            # Get mood with max value
            data['mood'] = max(mood, key=mood.get)
            data['bpm'] = int(bpm)
            data['danceability'] = danceability
            data['tonality'] = {
                'key': key,
                'scale': scale
            }
            data['requestId'] = username

        try:
            # producer.send("audio_analysis",  bytes(str(data), 'utf-8'))
            producer.send("analysis-query_params",  bytes(json.dumps(data), 'utf-8'))
            producer.flush()
            print("Sent to Kafka")
        except Exception as e:
            print("Error:", e)
            return Response("Error: " + str(e), status=400, mimetype='application/json')

    return jsonify(str(data)) 


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=ESSENTIA_SERVER_PORT)
            


