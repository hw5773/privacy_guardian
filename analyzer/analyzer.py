import os
import sys
from key_value_finder import kvl_finder
from package_name_finder import name_finder
from key_store import db_store

java_files = []                # the list of java files
rel_set = []                # the relation list
curr_class = ""                # the name of the current class

keywords = ["try", "catch", "while", "switch", "else"]
primitives = ["String", "int", "float", "double", "long", "boolean"]
permissions = ["Location.getLongitude", "Location.getLatitude"]

def represent_int(s):
    try:
        int(s)
        return True
    except ValueError:
        return False

def parse_left(left, cname):
    left_lst = left.strip().split(" ")
    ret = {}
    ret["var"] = cname + "." + left_lst[-1]
    ret["r2c"] = {}            # reference to class

    if len(left_lst) >= 2:
        pre = left_lst[-2].strip()
        if pre not in primitives:
            ret["r2c"][ret["var"]] = pre
        else:
            print ("it is primitive: %s" % pre)
    
    return ret

def parse_right(var, right, cname, r2c, i2c):
    s1 = set([])
    print ("  added val: ", var)
    s1.add(var)
    ret = {}
    ret["i2c"] = dict(i2c)    # instance to class

    if "new" in right:
        idx = right.index("new")
        num = idx + 3
        if "[" in right:
            val = right[num:num+right[num:].index("[")].strip() + "[]"
        elif "(" in right:
            val = right[num:num+right[num:].index("(")].strip()
            ret ["i2c"][var] = val
        print ("  added val: %s" % val)
        s1.add(val)
        
    elif right[0] == "(":
        val = right[right.index(")")+1:].strip()
        if "." in val:
            idx = val.index(".")
            obj = cname + "." + val[0:idx]
            val = val.replace(val[0:idx], obj)

            if obj in i2c.keys():
                val = val.replace(obj, i2c[obj])
            elif obj in r2c.keys():
                val = val.replace(obj, r2c[obj])
        print ("  added val: %s" % val)
        s1.add(val)

        try:
            idx1 = val.index("(")
        except:
            idx1 = -1

        if idx1 > 0:
            try:
                idx2 = val.index(")")
            except:
                idx2 = -1
            if idx2 > 0:
                val = cname + "." + val[idx1+1:idx2] #TODO: Need to cover the lists
                print ("  added val: %s" % val)
                s1.add(val)

    elif "." in right:
        idx = right.index(".")
        obj = cname + "." + right[0:idx]
        right = right.replace(right[0:idx], obj)
        print ("right: %s, obj: %s" % (right, obj))
        if obj in i2c.keys():
            print ("it's in i2c")
            right = right.replace(obj, i2c[obj])
        elif obj in r2c.keys():
            print ("it's in r2c")
            right = right.replace(obj, r2c[obj])
        print ("r2c keys: ", r2c.keys())
        print ("i2c keys: ", i2c.keys())

        if "(" in right and ")" in right:
            idx1 = right.index("(")
            idx2 = right.index(")")
            tmp = '%s' % right
            right = right[0:idx1]

            if idx2 - idx1 > 1:
                arg = cname + "." + tmp[idx1+1: idx2] # TODO: Need to process arg in detail
                print ("  added arg: %s" % arg)
                s1.add(arg)

        print ("revised right: %s" % right)

        val = right

        if "String.valueOf" not in right:
            print ("  added val: %s" % val)
            s1.add(val)
    else:
        if represent_int(right):
            val = right
        elif isinstance(right, str):
            val = cname + "." + right
        
        s1.add(val)

    add_rel(s1)
    return ret

def parse_line(line, cname, fname, r2c, i2c):
    line = line.strip()
    lst = line.split(" ")
    ret = {}
    ret["r2c"] = dict(r2c)
    ret["i2c"] = dict(i2c)

    if "=" in line:
        idx = line.index("=")
        left = line[0:idx].strip()
        left_ret = parse_left(left, cname)

        if len(left_ret["r2c"]) > 0:
            for k in left_ret["r2c"].keys():
                ret["r2c"][k] = left_ret["r2c"][k]

        right = line[idx+1:].strip()
        right_ret = parse_right(left_ret["var"], right, cname, ret["r2c"], ret["i2c"])

        if len(right_ret["i2c"]) > 0:
            for k in right_ret["i2c"].keys():
                ret["i2c"][k] = right_ret["i2c"][k]

    elif "return" in line:
        if line == "return":
            val = cname + "."
        else:
            idx = line.index(" ")
            val = line[idx+1:]
            lst = val.strip().split(" ")
        
            if "." in val:
                idx2 = val.index(".")
                obj = val[0:idx2].strip()
                if obj == "this":
                    val = val.replace(obj, cname)
                elif obj in i2c.keys():
                    val = val.replace(obj, i2c[obj])
                elif obj in r2c.keys():
                    val = val.replace(obj, r2c[obj])
            elif len(lst) >= 2:
                val = cname + "." + val
            elif len(lst) == 1:
                val = cname + "." + val

        s1 = set([])
        print ("  add rel_set: %s, %s" % (cname + "." + fname, val))
        s1.add(cname + "." + fname)
        s1.add(val)
        add_rel(s1)

    elif len(lst) >= 2:
        if lst[-2] not in primitives:
            print ("  add dict [r2c]: %s -> %s" % (cname + "." + lst[-1], lst[-2]))
            ret["r2c"][cname + "." + lst[-1]] = lst[-2]

    return ret
        
def add_rel(s1):
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

def parsing(blk, cname, fname, ref_to_class, inst_to_class):
    c = ""             # current character
    l = ""             # current line
    i = 0              # counter

    skip = False    # if the current byte is in the single-line comment
    comment = False    # if the current byte is in the multi-line comment
    in_str = False    # if the current byte is in the string

    while len(blk) > 0:
        well_taken = False    # to avoid multi-blanks

        while not well_taken:
            c = blk[0]
            blk = blk[1:]
            if len(blk) > 1:
                n = blk[0]
            else:
                break

            if c == " " and n == ".":
                c = n
                blk = blk[1:]
                well_taken = True
            if not (c == " " and n == " "):
                well_taken = True

        if skip:                                # cancel ignoring the annotation
            if c == "\n":
                skip = False
            continue
        elif comment:
            if len(blk) > 1:
                n = blk[0]
            if c == "*" and n == "/":        # ignore the multi-line comment
                comment = False
                blk = blk[1:]
            continue
        else:
            if len(blk) > 1:
                n = blk[0]
            if c == "@":                        # ignoring the annotation
                skip = True
                l = ""
                continue
            elif c == "\"":                        # to control the comment signature in the comment
                if in_str == True:
                    in_str = False
                else:
                    in_str = True
            elif c == "/" and n == "/" and not (in_str == True):    
                # ignore the single-line comment
                skip = True
                l = ""
                blk = blk[1:]
                continue
            elif c == "/" and n == "*":    # ignoring the multi-line comment
                comment = True
                blk = blk[1:]
                l = ""
                continue
            elif c == "\n" or c == "\r":
                continue
            elif c == ";":                        # when the line ends
                if "for " in l:
                    l = l + c
                    continue
                elif "import " in l:
                    print ("\n\nline: %s" % l.strip())
                    l = ""
                    continue
                elif "package " in l:
                    print ("\n\nline: %s" % l.strip())
                    l = ""
                print ("\n\nline: %s" % l.strip())
                ret = parse_line(l, cname, fname, ref_to_class, inst_to_class)

                for k in ret["r2c"].keys():
                    ref_to_class[k] = ret["r2c"][k]
                for k in ret["i2c"].keys():
                    inst_to_class[k] = ret["i2c"][k]

                print ("ref_to_class: ", ref_to_class)
                print ("inst_to_class: ", inst_to_class)
    
                l = ""
                continue
            elif c == "{":                        # when the block starts
                stack = [1]
                b = ""
                l = l.strip()

                while len(stack) > 0:
                    c = blk[0]
                    blk = blk[1:]
                    if c == "{":
                        stack.append(1)
                    elif c == "}":
                        stack.pop()

                    if len(stack) > 0:
                        b = b + c

                # to process the statements before the block
                if l == "":
                    parsing(b, cname, fname, ref_to_class, inst_to_class)
                elif l == "static":
                    parsing(b, cname, fname, ref_to_class, inst_to_class)
                elif l == "do":
                    parsing(b, cname, fname, ref_to_class, inst_to_class)
                elif "synchronized" in l:
                    parsing(b, cname, fname, ref_to_class, inst_to_class)
                elif "class" in l:                # when it is a class
                    class_name = parse_class(l)    # getting the class name
                    # parsing with the new class name
                    parsing(b, class_name, fname, ref_to_class, inst_to_class)
                elif "for" in l:                # when it is a for-statement
                    parsing(b, cname, fname, ref_to_class, inst_to_class)
                elif ("if" in l) or ("else" in l):
                    # when it is a conditional-statement
                    parsing(b, cname, fname, ref_to_class, inst_to_class)
                elif "try" in l:                # when it says "try"
                    parsing(b, cname, fname, ref_to_class, inst_to_class)
                elif "new" in l:
                    parsing(b, cname, fname, ref_to_class, inst_to_class)
                elif "catch" in l:                # when it says "catch"
                    ret = parse_catch(cname, l)
                    r2c = dict(ref_to_class)
                    for k in ret["r2c"].keys():
                        r2c[k] = ret["r2c"][k]
                    parsing(b, cname, fname, r2c, inst_to_class)
                elif "switch" in l:                # when it says "switch"
                    parse_none(l) # TODO: Need to implement the parser related to the "switch"
                elif "=" in l:                    # to process the array
                    line = l + " {" + b + "}"
                    ret = parse_line(line, cname, fname, ref_to_class, inst_to_class)
                    for k in ret["r2c"].keys():
                        ref_to_class[k] = ret["r2c"][k]
                    for k in ret["i2c"].keys():
                        inst_to_class[k] = ret["i2c"][k]
                else:
                    ret = parse_func(cname, l)
                    r2c = dict(ref_to_class)
                    for k in ret["r2c"].keys():
                        r2c[k] = ret["r2c"][k]
                        
                    parsing(b, cname, ret["func_name"], r2c, inst_to_class)

                l = ""
                continue

            l = l + c

def parse_none(prefix):
    print ("Not Implemented Yet")
    return 0

def parse_catch(cname, prefix):
    ret = {}
    idx1 = prefix.index("(")
    idx2 = prefix.index(")")
    params = prefix[idx1+1:idx2]
    if len(params) >= 1:
        ret["r2c"] = parse_params(cname, params)

    return ret

def parse_class(prefix):
    print ("lst in parse_class: ", prefix)
    lst = prefix.split(" ")
    idx = lst.index("class")
    idx = idx + 1
    return lst[idx]

def parse_func(cname, prefix):
    idx = 1
    ret = {}
    ret["func_name"] = ""
    ret["r2c"] = {}
    
    while idx > 0:
        try:
            idx = prefix.index(" ")
            idx2 = prefix.index("(")
            if idx < idx2:
                prefix = prefix[idx+1:]
            else:
                if len(prefix[0:idx2].strip()) > 0:
                    ret["func_name"] = prefix[0:idx2]
                break
        except:
            idx = -1
            print ("prefix: ", prefix)
            idx2 = prefix.index("(")
            ret["func_name"] = prefix[0:idx2]
            print ("func_name: ", ret["func_name"])

    idx1 = prefix.index("(")
    idx2 = prefix.index(")")
    params = prefix[idx1+1:idx2]

    if len(params) >= 1:
        ret["r2c"] = parse_params(cname, params)

    return ret

def parse_params(cname, params):            # parsing the parameters - "String a, int b, ..."
    lst = params.split(",")
    r2c = {}
    for p in lst:
        idx = p.strip().index(" ")
        c = p[0:idx].strip()
        r = p[idx+1:].strip()
        r2c[cname + "." + r] = c

    return r2c

def find_location_lst():
    ret = []
    for p in permissions:
        for lst in rel_set:
            if p in lst:
                ret.append((p, lst))

    return ret
        

def key_extractor(lst, app_dir):
    kvl_lst = kvl_finder(app_dir)
    ret = []

    print ("lst in key_extractor: ", lst)

    for k, v in kvl_lst.items():
        for e in v:
            for elem in lst:
                if e in elem[1]:
                    ret.append(k + "_" + str(elem[0]))

    return ret

def usage():
    print ("python3 analyzer.py <directory name of the app>")
    exit(1)

def main():
    if len(sys.argv) != 2:
        usage()

    app_dir = sys.argv[1]

    for root, dirs, files in os.walk(app_dir):
        for f in files:
            if ".java" in f:
                java_files.append(root + "/" + f)

        print ("java_files: ", java_files)

    for f in java_files:
        fi = open(f, "r")
        b = fi.read()

        print (fi)
        parsing(b, "", "", {}, {})
        print ("\n")
    
    print ("rel_set: ", rel_set)
    loc_lst = find_location_lst()
    print ("loc_lst: ", loc_lst)
    keys = key_extractor(loc_lst, app_dir)
    name = name_finder(app_dir)
    fmt = "json"
    print ("name: ", name)
    print ("keys: ", keys)
    #db_store(name, keys, fmt)
    

if __name__ == "__main__":
    main()

