from flask import Flask, url_for
from flask import request
from flask import jsonify
from flask import json

app = Flask(__name__)

@app.route('/lastupdate', methods = ['GET'])
def api_lastupdate():
	if request.method == 'GET':
		return "1474963999"

@app.route('/sensitiveinfo', methods = ['GET'])
def api_sensitiveinfo():
	if request.method == 'GET':
		return jsonify({"List": [{"AppId":"org.locationprivacy.locationprivacy", "Format":"json", "HookTarget":[{"Keyword":"latitude", "Type":"latitude"}, {"Keyword":"longitude", "Type":"longitude"}], "Timestamp":"1474960000"}, {"AppId":"testapp2", "Format":"json", "HookTarget":[{"Keyword":"lat", "Type":"GPSInfo"}, {"Keyword":"lon", "Type":"GPSInfo"}], "Timestamp":"1470889243"}]})

if __name__ == '__main__':
	app.run(host="147.46.215.152", port=2507)
