#!/usr/bin/env python3
# -*- coding: utf-8 -*-


import socket
import struct
import random
import threading


#the client class contains all the sockets and send/read utilities
# for the client process 
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
        
        #bind udp socket
        self.udp_sock.bind(('',0))
   
    #sends the message through the udp socket
    
    def Send(self,message,address):
        message = message
        sent = self.udp_sock.sendto(message, address)
        return sent
    
    #reads the messages through the udp socket
    def Read(self):
        data, server = self.udp_sock.recvfrom(1024)
        return data,server
    
    #sends the message through the multicast registered socket
    def multiSend(self,message):
        message = message.encode()
        sent = self.sock.sendto(message, self.multicast_group)
        return sent
    
    #reads the message through the multicast registered socket
    def multiRead(self):
        data, server = self.sock.recvfrom(1024)
        return data.decode(),server
    
    #closes the multcast socket
    def mulsockClose(self):
        self.sock.close()
        del self.sock
    
    #closes the udp socket
    def udpsockClose(self):
        self.udp_sock.close()
        del self.udp_sock

#class that contains the information of the request
class Request:
    def __init__(self,svcid,buffer,length):
        self.svcid = svcid
        self.buffer = buffer
        self.length = length
        self.key =  random.randint(1,1000000000)
        self.sem= threading.Semaphore(0)
    
    #returns the information as dictionary    
    def asDict(self):
        return{'key':self.key, 'svcid':self.svcid, 'buffer':self.buffer, 'len':self.length , 'sem':self.sem}
    
    #return the key of this request
    def getKey(self):
        return(self.key)    

                    ####global variables###
                    
MAX_SIZE = 15    #Max size of buffer
clt_buffer = []  #The client buffer
multicast_group = ('226.1.1.1',10000); 
clnt = client(multicast_group) #initialation of client class
reply_buffer = [] #Buffer that contains all the replies


#finds in the reply buffer the dictionary that contains the key
#and returns  length,reply and the index of the dictinary 
#that contains the key

def find_reply(key):
    for i in range(len(reply_buffer)):
        if(reply_buffer[i].get("key") == key):
            buffer = reply_buffer[i].get("buffer")
            lenght = reply_buffer[i].get("lenght")
            #reply_sem.release()
            return lenght,buffer,i
    return -1,-1,-1


#finds in the buffer the dictionary that contains the key
#and returns the index of the dictinary that contain this key in 

def findRequest(key):
    
    #reply_sem.acquire()
    for i in range(len(reply_buffer)):
        if(clt_buffer[i].get("key") == key):
            return i
    #reply_sem.release()
    return -1
        

#finds in buffer if we have a duplicate

def findDuplicateSend(svcid,length,buffer):
    for i in range(len(clt_buffer)):
        if(clt_buffer[i].get("svcid") == svcid):
            if(clt_buffer[i].get("buffer") == buffer):
                if(clt_buffer[i].get("len") == length):
                    return True
    return False

#finds in reply_buffer if we have a duplicate
def findDuplicateRec(key,length,buffer):
    for i in range(len(reply_buffer)):
        if(reply_buffer[i].get("key") == key):
            if(reply_buffer[i].get("buffer") == buffer):
                if(reply_buffer[i].get("lenght") == length):
                    return True
    
    return False


#Makes the message that will send to server as request 
#The form of message id [4bytes:key,4bytes:length,length bytes:buffer ]
        
def zip_b(key,length,buffer):
    
    new_key = key.to_bytes(4,byteorder="big")
    new_length = length.to_bytes(4,byteorder="big")
    new_buffer = buffer.to_bytes(length,byteorder="big")
    
    send_buffer = new_key
    send_buffer += new_length
    send_buffer += new_buffer
   
    return send_buffer

#Takes the information of the reply send by server

def unzip(data):
    
    x1 = data[0:4]
    key = int.from_bytes(x1,"big")
  #  print(key)
    x2 = data[4:8]
    length = int.from_bytes(x2,"big")
   # print (length)
    buffer  = data[8:8+length]
    
    #print(buffer)
    
    return {"key":key,"lenght":length,"buffer":buffer}


#Discovery of the server via multicast
def multicastProtocol(message):
    
    counter = 0
    print("Sending " + message + " in multicast")
    clnt.multiSend(message)
    # Look for responses from all recipients
    while(counter <  2):
        try:
            data, server = clnt.multiRead()
        except socket.timeout:
            print('Timed out in multicast Protocol no more responses')
            counter = counter + 1
            clnt.multiSend(message)
        else:
            print('received "%s" from %s' % (data, server))
            break
    
    if(counter == 2):
        print("server error in multicastProtocol")
        return -1
    
    return server


#sends the request to server and waits for acknolegment
def sendWithAck(message,server,sem,key):
    
    counter = 0
    
    print("sending data:")
    print(message)
    print("in server:")
    print(server)
    reply_sem.acquire()
    clnt.Send(message,server)
    reply_sem.release()
    
    #look for responce to mesage
    
    while(counter <  2):
        check = sem.acquire(blocking=True, timeout=2)
        if(check == False):
            print('timed out in sending request_%d no more responses for Ack_%d'% (key, key))
            counter = counter + 1
            clnt.Send(message,server)
        else:
            break
    
    if(counter == 2):
        print("server error in sending request_%d" % (key))
        return -1
    
    return 1


    
    
def sendRequest(svcid,buffer,length):
    
    multicast_request = "RR_CLIENT_"
    
    svc_str = str(svcid)
    
    multicast_request += svc_str
    
    req = Request(svcid,buffer,length)
    
    if(len(clt_buffer) == MAX_SIZE):
        return -1
    
    if(findDuplicateSend(svcid,length,buffer) == True):
        return -1
    
    clt_buffer.append(req.asDict())
    
    send_msg = zip_b(req.getKey(),length,buffer)
    
    server = multicastProtocol(multicast_request)
    
    if(server == -1):
        return-1
    
    print("multicast discovery succesful")
    
    ack = sendWithAck(send_msg,server,req.asDict().get("sem"),req.getKey())
    
    if(ack == -1):
        return -1
        
    return req.getKey()


#Tread that has a while(1) and reads the data that comes from server 

class readThread(threading.Thread):
    def __init__(self):
        super(readThread,self).__init__()
        
    def run(self):
        while(1):
            data,server = clnt.Read()
            # if the read from server is an ack release the semaphore so 
            # that we know for sure the server has our request
            # and stop retransmit
            try:
                
                new_data = data
                ack = new_data[0:4].decode()
                
                if(ack == "Ack_"):
                    key = int(new_data[4:len(new_data)])
                    print("Received Akc_%d from server %s" % (key,server))
                    index = findRequest(key)
                    sem = clt_buffer[index].get("sem")
                    sem.release()
                else:
                    dict_reply = unzip(data)
                
                    if(findDuplicateRec(dict_reply.get("key"),dict_reply.get("lenght"),dict_reply.get("buffer")) == False):
                        print("Received relpy_%d from server %s" % (dict_reply.get("key"),server))    
                        reply_buffer.append(dict_reply)
                        index = findRequest(dict_reply.get('key'))
                        clt_buffer.remove(clt_buffer[index])
                        print("sending ACK_RECIVED in server")
                        reply_sem.acquire()
                        clnt.Send("Ack_recived".encode(),server)
                        reply_sem.release()
                    else:
                        print("Received duplicate relpy_%d from server %s" % (dict_reply.get("key"),server))
                        print("sending ACK_RECIVED in server :%s due to a duplicate" % (server))
                        reply_sem.acquire()
                        clnt.Send("Ack_recieved".encode(),server)
                        reply_sem.release()
                    
                    
            except UnicodeDecodeError:
                
                dict_reply = unzip(data)
                
                if(findDuplicateRec(dict_reply.get("key"),dict_reply.get("lenght"),dict_reply.get("buffer")) == False):
                    print("Received relpy_%d from server %s" % (dict_reply.get("key"),server))    
                    reply_buffer.append(dict_reply)
                    index = findRequest(dict_reply.get('key'))
                    clt_buffer.remove(clt_buffer[index])
                    print("sending ACK_RECIVED in server")
                    reply_sem.acquire()
                    clnt.Send("Ack_recived".encode(),server)
                    reply_sem.release()
                else:
                    print("Received duplicate relpy_%d from server %s" % (dict_reply.get("key"),server))
                    print("sending ACK_RECIVED in server :%s due to a duplicate" % (server))
                    reply_sem.acquire()
                    clnt.Send("Ack_recieved".encode(),server)
                    reply_sem.release()

def getReply(reqid,block):    
    
    if(block == True):
        buffer = -1
        while(1):
            lenght,buffer,index = find_reply(reqid)
            if(buffer != -1):
                reply_buffer.remove(reply_buffer[index])
                break
        return lenght,buffer
    else:
        lenght,buffer,index = find_reply(reqid)
       
        if(buffer != -1):
                reply_buffer.remove(reply_buffer[index])
        
        return lenght,buffer



#sem = threading.Semaphore(0)
reply_sem = threading.Semaphore(1)
thread = readThread()
thread.start()

  
    