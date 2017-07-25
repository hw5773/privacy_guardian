import os

def pem_lst():
    lst = []
    for root, dirs, files in os.walk("/etc/ssl/certs"):
        for f in files:
            if ".pem" in f:
                lst.append(f)
    return lst

def main():
    lst = pem_lst()
    print (lst)
    i = 0

    for cert in lst:
        alias = "cert" + str(i)
        cmd = "keytool -importcert -keystore my_keystore -storepass mmlab2015 -trustcacerts -alias " + alias + " -file /etc/ssl/certs/" + str(cert)
        os.system(cmd)
        i = i + 1

if __name__ == "__main__":
    main()
