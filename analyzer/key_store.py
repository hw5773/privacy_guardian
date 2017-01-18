import pymysql
import os
import sys
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from etc.etc import db_connect

def db_store(name, keys, fmt):
	conn = db_connect()
	cur = conn.cursor()

	query = "insert into app_keylist(app_id, key_list, format) values('" + name + "','" + '/'.join(keys) + "','" + fmt + "');"

	print ("query: ", query)

	cur.execute(query)

	cur.close()
	conn.close()
