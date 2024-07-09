# Import needed modules from osc4py3
import py_eureka_client.eureka_client as eureka_client
from flask import Flask, request, jsonify, Response
from kafka import KafkaProducer
from pprint import pprint

producer = KafkaProducer(bootstrap_servers='localhost:19092')

from tempfile import TemporaryDirectory
# import essentia.standard as es

prefix = "/mnt/c/Users/jacot/Music/"

ESSENTIA_SERVER_PORT = 9090

# eureka_client.init(
#     eureka_server="http://user:password@localhost:8761",
#     app_name="ESSENTIA-SERVICE",
#     instance_port=ESSENTIA_SERVER_PORT

# )
app = Flask(__name__) 

@app.route('/audio_analysis', methods=['GET', 'POST'])
def audio_analysis(file=None):
    print("audio_analysis():")
    # Get the file from the request
  
    data = {}
    if request.method == 'POST':
        if 'file' not in request.files:
            return Response("No file part", status=400, mimetype='application/json')
        try:
            file = request.files['file']
            print("file:", file)
            if file.filename == '':
                return Response("No selected file", status=400, mimetype='application/json')
            else:
                data['filename'] = file.filename
            
        except Exception as e:
            print("Error:", e)
            return Response("Error: " + str(e), status=400, mimetype='application/json')
  
    # data = analyze(file)
    result = str(data)
    try:
        producer.send("audio_analysis", result.encode('utf-8'))
        producer.flush()
        print("Sent to Kafka")
    except Exception as e:
        print("Error:", e)
        return Response("Error: " + str(e), status=400, mimetype='application/json')
    return Response(result, status=200, mimetype='application/json')

@app.route('/audio/test', methods=['GET'])
def audio_test():
    print("audio_test():")
    return jsonify({"message": "audio_test()"})

def analyze(file):
    # do the analysis
    with TemporaryDirectory() as tmpdirname:
        print('Created temporary directory:', tmpdirname)
        file.save(tmpdirname + "/audio.mp3")
        # audio = es.MonoLoader(filename=tmpdirname + "/audio.mp3")()
        # print("audio:", audio)
        # Extract features
        # features, features_frames = es.MusicExtractor(lowlevelSilentFrames='drop', lowlevelFrameSize=2048, lowlevelHopSize=1024, tonalFrameSize=4096, tonalHopSize=2048)(tmpdirname + "/audio.mp3")
        # print("features:", features)
        # print("features_frames:", features_frames)
        # data = {
        #     "features": features,
        #     "features_frames": features_frames
        # }
    
    return "data"

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=ESSENTIA_SERVER_PORT)
            



