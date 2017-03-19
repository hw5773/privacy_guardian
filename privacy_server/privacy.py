from flask import Flask, url_for
from flask import request
from flask import jsonify
from flask import json
import os
import sys
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from etc.etc import db_connect

app = Flask(__name__)

@app.route('/lastupdate', methods = ['GET'])
def api_lastupdate():
	if request.method == 'GET':
		conn = db_connect()
		cur = conn.cursor()
		query = "select max(UNIX_TIMESTAMP(reg_time)) from app_keylist;"
		cur.execute(query)

		time = cur.fetchall()[0][0]
		print ("lastupdate: ", time)

		cur.close()
		conn.close()

		return str(time)
#		return "1474963999"

@app.route('/sensitiveinfo', methods = ['GET'])
def api_sensitiveinfo():
	if request.method == 'GET':
		conn = db_connect()
		cur = conn.cursor()
		query = "select id, app_id, UNIX_TIMESTAMP(reg_time), key_list, format from app_keylist;"
		cur.execute(query)

		rows = cur.fetchall()
		result = []

		for r in rows: # 0: id, 1: app_id, 2: reg_time, 3: key_list, 4: format
			print ("r: ", r)
			key_lst = []
			keys = r[3].split("/")
			print ("keys: ", keys)

			for k in keys:
				kt = k.split("_")
				key_lst.append({"Keyword": kt[0], "Type": kt[1]})
				print ("key_lst: ", key_lst)

			result.append({"AppId":r[1], "Format": r[4], "HookTarget": key_lst, "Timestamp": r[2]})

		print ("result: ", result)
		result_lst = {"List": result}

#		result = {"List": [{"AppId":"org.locationprivacy.locationprivacy", "Format":"json", "HookTarget":[{"Keyword":"latitude", "Type":"latitude"}, {"Keyword":"longitude", "Type":"longitude"}], "Timestamp":"1474960000"}, {"AppId":"testapp2", "Format":"json", "HookTarget":[{"Keyword":"lat", "Type":"GPSInfo"}, {"Keyword":"lon", "Type":"GPSInfo"}], "Timestamp":"1470889243"}]}
		return jsonify(result_lst)

if __name__ == '__main__':
	app.run(host="147.46.215.152", port=2507)
