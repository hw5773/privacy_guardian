import os

java_files = []
rel_set = []
instance_to_class = {}
curr_class = ""

for root, dirs, files in os.walk("."):
	for f in files:
		if ".java" in f:
			java_files.append(f)

print "java_files: "
print java_files

print"\nrel_set: "
for f in java_files:
	fi = open(f, "r")

	stmt = ""

	for line in fi:
		stmt = (stmt + " " + line).strip()

		curr_func = ""

		while ";" in stmt:
			idx = stmt.index(";")
			command = stmt[0:idx].strip()
			stmt = stmt[idx+1:].strip()

			s1 = set([])
			if "{" in command:
				command_lst = command.split()

				if "class" in command_lst:
					idx = command_lst.index("class")
					curr_class = command_lst[idx+1].strip()

				idx = command.index("{")

				if ")" in command:
					idx2 = command.index(")")

					if idx2 < idx:
						idx1 = command.index("(")
						func_name = command[0:idx1].strip()

						try:
							blank = func_name.index(" ")
							while blank >= 0:
								func_name = func_name[(blank+1):]
								blank = func_name.index(" ")
						except:
							curr_func = func_name.strip()
						curr_func = func_name.strip()
 
				command = command[idx+1:].strip()
				print "command: %s" % command
				print "curr_func: %s" % curr_func

			if "return" in command:
				idx = command.index("return")
				command = command[idx:].strip()
				var1 = curr_func
				idx = command.index(" ")
				var2 = command[idx+1:].strip()

				s1 = set([])
				if var1 or (var1 != "null") or (var1 != ""):
					s1.add(var1)
				if var2 or (var2 != "null") or (var2 != ""):
					s1.add(var2)

				print "s1: "
				print s1
	
				contain = False
				for elem in rel_set:
					s2 = set(elem)

					if s1 & s2:
						contain = True
						rel_set.remove(elem)
						rel_set.append(list(s1 | s2))

				if not contain:
					if list(s1):
						rel_set.append(list(s1))

				curr_func = ""

			if "=" in command:
				command_lst = command.split()
				try:
					idx = command_lst.index("=")
				except:
					idx = -1

				if idx >= 0:	
					var1 = command_lst[idx-1]
					idx = idx + 1
					var2 = command_lst[idx]
					if var2 == "new":
						idx = idx + 1
						var2 = command_lst[idx]

					if var2[0] == "(":
						idx = idx + 1
						var2 = command_lst[idx]

					try:
						idx1 = var2.index("(")
					except:
						idx1 = -1

					if var1:
						s1.add(var1)

					if var2:
						s1.add(var2)

					if idx1 > 0:
						try:
							idx2 = var2.index(")")
						except:
							idx2 = -1
						if idx2 > 0:
							var2 = var2[idx1+1:idx2]

					if var2:
						s1.add(var2)

				contain = False
				for elem in rel_set:
					s2 = set(elem)

					if s1 & s2:
						contain = True
						rel_set.remove(elem)
						rel_set.append(list(s1 | s2))

				if not contain:
					if list(s1):
						rel_set.append(list(s1))

	fi.close()

for s in rel_set:
	print s
