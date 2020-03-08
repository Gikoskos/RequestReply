#!/usr/bin/env python3
# -*- coding: utf-8 -*-


import client_s3 as clt
import time

primes = [8447,6311,941027,909631,49022843,59366651,470202691643,722082735347]
key = []

for i in range(len(primes)):
    key.append(clt.sendRequest(44,primes[i],8))
    time.sleep(1)
#len(primes)
for i in range(len(primes)):
    length, is_prime = clt.getReply(key[i],True)
    if is_prime != 0:
        print("\n{} is prime!".format(primes[i]))
    else:
        print("\n{} isn't prime!".format(primes[i]))
    
        
