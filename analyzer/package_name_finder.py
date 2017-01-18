import os
from bs4 import BeautifulSoup

def name_finder(d):
	for root, dirs, files in os.walk(d):
		for fi in files:
			if "AndroidManifest.xml" in fi:
				f = open(root + "/" + fi, "r")
				xml = f.read()
				soup = BeautifulSoup(xml, 'lxml')
				name = soup.manifest['package']

				f.close()

	return name

def main():
	xmls = name_finder("basic_app")
	print (xmls)

if __name__ == "__main__":
	main()
