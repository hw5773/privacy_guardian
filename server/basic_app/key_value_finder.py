import os

java_files = []
var_lst = []
kvl_lst = {}

for root, dirs, files in os.walk("."):
	for f in files:
		if ".java" in f:
			java_files.append(f)

print "java_files: "
print java_files

for f in java_files:
	fi = open(f, "r")
	cname = f.split(".")[0]
	stmt = ""

	for line in fi:
		stmt = (stmt + " " + line).strip()

		while ";" in stmt:
			idx = stmt.index(";")
			command = stmt[0:idx]
			stmt = stmt[idx+1:]

			if "{" in command:
				idx = command.index("{")
				command = command[idx+1:].strip()

			if "JSONObject" in command:
				command_lst = command.split()
				try:
					idx = command_lst.index("JSONObject")
				except:
					idx = -1
				if idx >= 0:
					var_lst.append(command_lst[idx+1])

			if "put" in command:
				command_lst = command.split(".")
				contain = False
				kv = ""

				for elem in command_lst:
					if elem in var_lst:
						contain = True
					if "put" in elem:
						kv = elem.strip()

				if contain:
					idx1 = kv.index("(")
					idx2 = kv.index(")")
					kv = kv[idx1+1:idx2]
					key = kv.split(",")[0].strip()
					value = cname + "." + kv.split(",")[1].strip()

					if key in kvl_lst.keys():
						kvl_lst[key].append(value)
					else:
						kvl_lst[key] = [value]
						
			
	fi.close()

print "var_lst: "
print var_lst
print "kvl_lst: "
print kvl_lst
