#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from queue import Queue
import socket
import struct
import random
import time
import threading
import logging


class client:
    
    def __init__(self,multicast_group):
        self.multicast_group = multicast_group
         
        #create the datagram socket
        self.sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
        
        #set timeout for multicast_socket
        self.sock.settimeout(2)
        
        #set the ttl to 1  
        self.ttl = struct.pack('b',1)
        self.sock.setsockopt(socket.IPPROTO_IP,socket.IP_MULTICAST_TTL,self.ttl)
       
        #create  udp socket
        self.udp_sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
        
        #set timeout for udp_socket
        self.sock.settimeout(2)
        
        #bind udp socket
        self.udp_sock.bind(('',0))
    
    def Send(self,message,address):
        message = message
        sent = self.udp_sock.sendto(message, address)
        return sent
    
    def Read(self):
        data, server = self.udp_sock.recvfrom(1024)
        return data,server
    
    def multiSend(self,message):
        message = message.encode()
        sent = self.sock.sendto(message, self.multicast_group)
        return sent
    
    def multiRead(self):
        data, server = self.sock.recvfrom(1024)
        return data.decode(),server
    
    def mulsockClose(self):
        self.sock.close()
        del self.sock
        
    def udpsockClose(self):
        self.udp_sock.close()
        del self.udp_sock
    


clt_buffer = Queue(maxsize=15)
multicast_group = ('226.1.1.1',10000);
clnt = client(multicast_group)
reply_buffer = []

def find_reply(key):
    
    #reply_sem.acquire()
    for i in range(len(reply_buffer)):
        if(reply_buffer[i].get("key") == key):
            buffer = reply_buffer[i].get("buffer")
            lenght = reply_buffer[i].get("lenght")
            #reply_sem.release()
            return lenght,buffer
    #reply_sem.release()
    return -1,-1
        
        


class Request:
    def __init__(self,svcid,buffer,length):
        self.svcid = svcid
        self.buffer = buffer
        self.length = length
        self.key =  random.randint(1,1000000000)
        
    def asDict(self):
        return{'key':self.key, 'svcid':self.svcid, 'buffer':self.buffer, 'len':self.length }
    
def zip_b(key,length,buffer):
    
    new_key = key.to_bytes(4,byteorder="big")
    new_length = length.to_bytes(4,byteorder="big")
    new_buffer = buffer.to_bytes(length,byteorder="big")
    
    send_buffer = new_key
    send_buffer += new_length
    send_buffer += new_buffer
   
    return send_buffer

def unzip(data):
    
    x1 = data[0:4]
    key = int.from_bytes(x1,"big")
  #  print(key)
    x2 = data[4:8]
    length = int.from_bytes(x2,"big")
   # print (length)
    x3 = data[8:8+length]
    buffer = int.from_bytes(x3,"big")
    #print(buffer)
    
    return {"key":key,"lenght":length,"buffer":buffer}


def multicastProtocol(message):
    
    counter = 0
    clnt.multiSend(message)
    print("waiting to receive")
    # Look for responses from all recipients
    while(counter <  3):
        try:
            data, server = clnt.multiRead()
        except socket.timeout:
            print('timed out, no more responses')
            counter = counter + 1
            clnt.multiSend(message)
        else:
            print('received "%s" from %s' % (data, server))
            break
    
    if(counter == 3):
        return -1
    
    counter = 0
    reply_sem.acquire()
    clnt.Send("client_ack".encode(),server)
    reply_sem.release()
    
    #look for responce to client_ack
    while(counter <  3):
        check = sem.acquire(blocking=True, timeout=2)
        if(check == False):
            print('timed out, no more responses')
            counter = counter + 1
            clnt.Send("client_ack".encode(),server)
        else:
            break
    
    if(counter == 3):
        return -1
    
    print('closing socket')
    clnt.mulsockClose()
    
    return server



def sendWithAck(message,server):
    
    counter = 0
    reply_sem.acquire()
    clnt.Send(message,server)
    reply_sem.release()
    
    #look for responce to mesage
    
    while(counter <  3):
        check = sem.acquire(blocking=True, timeout=2)
        if(check == False):
            print('timed out, no more responses')
            counter = counter + 1
            clnt.Send("client_ack".encode(),server)
        else:
            break
    
    if(counter == 3):
        return -1
    
    return 1


    
    
        
def sendRequest(svcid,buffer,length):
    multicast_request = "RR_CLIENT_"
    
    svc_str = str(svcid)
    
    multicast_request += svc_str
    
    req = Request(svcid,buffer,length)
    
    if(clt_buffer.full()):
        return -1
    
    clt_buffer.put(req.asDict())
    
    send_msg = zip_b(req.asDict().get('key'),length,buffer)
    
    server = multicastProtocol(multicast_request)
    
    if(server == -1):
        return-1
    
    ack = sendWithAck(send_msg,server)
        
    return req.asDict().get('key')




class readThread(threading.Thread):
    def __init__(self):
        super(readThread,self).__init__()
        
    def run(self):
        while(1):
            #reply_sem.acquire()
            data,server = clnt.Read()
            #print(sem._value)
            try:
                new_data = data.decode()
                #print(new_data)
                if(new_data == "Ack"):
                    #print("aaaaaaaaaaa")
                    #reply_sem.release()
                    sem.release()
            except UnicodeDecodeError:
                print("aaaaaaaaaaa")
                dict_reply = unzip(data)
                reply_buffer.append(dict_reply)
                reply_sem.acquire()
                clnt.Send("Ack".encode(),server)
                reply_sem.release()
#--------------------anzip and in reply buffer-------------------------#
                #key,data = unzip(data)
                
def getReply(reqid,block):
    
    if(block == True):
        buffer = -1
        while(buffer == -1):
            lenght,buffer = find_reply(reqid)
        return lenght,buffer
    else:
        lenght,buffer = find_reply(reqid)
        return lenght,buffer


sem = threading.Semaphore(0)
reply_sem = threading.Semaphore(1)
thread = readThread()
thread.start()
key = sendRequest(44,55376257,8)
print(key)
lenght,buffer = getReply(key,True)    
print(lenght)
print(buffer)