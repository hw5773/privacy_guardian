from flask import Flask, url_for
from flask import request
from flask import jsonify
from flask import json

app = Flask(__name__)

@app.route('/location', methods = ['GET', 'POST'])
def api_location():
	if request.method == 'GET':
		return jsonify({"message":"test_get", "latitude":38, "longitude": 100})

	elif request.method == 'POST':
		js = request.json[0]
		lat = js.get("latitude")
		lon = js.get("longitude")

		print ("JSON message: " + json.dumps(js))
		print ("Latitude: " + lat)
		print ("Longitude: " + lon)
		return jsonify({"message":"test_post", "latitude":lat, "longitude":lon})

if __name__ == "__main__":
	app.run(host="147.46.215.152", port=7979)
