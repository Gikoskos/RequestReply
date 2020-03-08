#!/usr/bin/env python3
# -*- coding: utf-8 -*-


import client as clt
import time
#from struct import *


primes = [32, 66, 77, 152, 8447,6311,941027,909631,49022843,59366651,470202691643,722082735347]
key = []

for i in range(len(primes)):
    key.append(clt.sendRequest(44,primes[i],8))
    time.sleep(1)
for i in range(len(primes)):
    is_prime, length = clt.getReply(key[i],True)
    #is_prime = unpack('>i', is_prime)
    is_prime = int.from_bytes(is_prime, byteorder='big', signed=False)
    if is_prime != 0:
        print("\n{} is prime!".format(primes[i]))
    else:
        print("\n{} isn't prime!".format(primes[i]))
    
