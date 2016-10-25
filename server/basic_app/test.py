import os
import sys

java_files = []				# the list of java files
rel_set = []				# the relation list
instance_to_class = {}		# the mapping between instance and class
curr_class = ""				# the name of the current class
keywords = ["try", "catch", "while"]


def parse_line(line, cname, fname):
	s1 = set([])
	if "=" in line:
		lst = line.split()
		try:
			idx = lst.index("=")
		except:
			idx = -1

		if idx >= 0:
			var1 = lst[idx-1]
			idx = idx + 1
			var2 = lst[idx]
			if var2 == "new":
				idx = idx + 1
				var2 = lst[idx]

			if var2[0] == "(":
				idx = idx + 1
				var2 = lst[idx]

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

	elif "return" in line:
		lst = line.split()
		s1.add(fname)
		s1.add(lst[1]) # value

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

def parse_before(line):
	print "in parse_before: %s" % line
	lst = line.split(" ")

	for word in lst:
		if word in keywords:
			return ""

	func_name = lst[-1].strip()
	try:
		if func_name.index("(") > 0:
			fname = func_name[0:idx]
	except:
		fname = lst[-2].strip()

	print "fname: %s" % fname
	return fname

def parsing(blk, cname, fname):
	c = ""
	l = ""
	i = 0

	skip = False
	comment = False

	while len(blk) > 0:
		c = blk[0]
		blk = blk[1:]

		if skip:
			if c == "\n":
				skip = False
			continue
		elif comment:
			if c == "*" and blk[0] == "/":
				comment = False
				blk = blk[1:]
			continue
		else:
			if c == "@":
				skip = True
				l = ""
				continue
			elif c == "/" and blk[0] == "/":
				skip = True
				l = ""
				blk = blk[1:]
				continue
			elif c == "/" and blk[0] == "*":
				comment = True
				blk = blk[1:]
				l = ""
				continue
			elif c == "(":
				stack = [1]
				b = c
				while len(stack) > 0:
					c = blk[0]
					blk = blk[1:]
					b = b + c
					if c == "(":
						stack.append(1)
					elif c == ")":
						stack.pop()
					
					if len(stack) == 0:
						c = b
			elif c == "\n" or c == "\r":
				continue
			elif c == ";":
				l = l.strip()
				parse_line(l, cname, fname)
				l = ""
				continue
			elif c == "{":
				stack = [1]
				b = ""
				l = l.strip()
				ftmp = parse_before(l)
				print "block start"
				while len(stack) > 0:
					c = blk[0]
					blk = blk[1:]
					if c == "{":
						stack.append(1)
					elif c == "}":
						stack.pop()

					if len(stack) > 0:
						b = b + c
				if len(ftmp) > 0:
					parsing(b, cname, ftmp)
				else:
					parsing(b, cname, fname)
				l = ""
				print "block end"
				continue

			l = l + c

for root, dirs, files in os.walk("."):
	for f in files:
		if ".java" in f:
			java_files.append(f)

print "java_files: "
print java_files

l = ""

java_files = ["HttpUtil.java"]

for f in java_files:
	fi = open(f, "r")
	cname = f.split(".")[0]
	l = fi.read()

	print fi
	parsing(l, cname, "")
	print "\n"

print rel_set
