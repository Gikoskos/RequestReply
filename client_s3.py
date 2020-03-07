#!/usr/bin/env python3
# -*- coding: utf-8 -*-


import socket
import struct
import random
import threading
import queue


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

         #set timeout for multicast_socket
        self.udp_sock.settimeout(2)
        #bind udp socket
        self.udp_sock.bind(('',0))
   
    #sends the message through the udp socket
    def setBlocking(self):
        self.udp_sock.settimeout(None)
    
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
multicast_group = ('226.1.1.1',10000)
reply_queue = queue.Queue(MAX_SIZE)
reply_dict = {}


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
    buffer = data[8:8+length]
    
    #print(buffer)
    
    return {
        "key": key,
        "length": length,
        "buffer": buffer
    }


#Discovery of the server via multicast
def multicastProtocol(clnt, message):
    
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
def sendWithAck(message,server,clnt,key):
    
    counter = 0
    
    print("sending data:")
    print(message)
    print("in server:")
    print(server)

    clnt.Send(message,server)
    #look for responce to mesage
    
    while(counter <  2):
        try:
            check = clnt.Read()
        except socket.timeout:
            print('timed out in sending request_%d no more responses for Ack_%d'% (key, key))
            counter = counter + 1
            clnt.Send(message,server)
        else:
            break
    
    if(counter == 2):
        print("server error in sending request_%d" % (key))
        return False
    
    return True


class Reply():
    def __init__(self):
        self.data = None
        self.length = 0

    def setData(self, data):
        self.data = data

    def setLength(self, length):
        self.length = length


class sendRequestThread(threading.Thread):
    def __init__(self,request, reply):
        threading.Thread.__init__(self)
        self.clnt = client(multicast_group)
        self.request = request
        self.reply = reply

    def run(self):
        multicast_request = "RR_CLIENT_"

        svc_str = str(self.request.svcid)
        
        multicast_request += svc_str

        multicast_request += "_"
        multicast_request += str(self.request.getKey())

        server = multicastProtocol(self.clnt, multicast_request)
        if (server == -1):
            reply_dict[self.request.getKey()][0].release()
            return -1

        send_msg = zip_b(self.request.getKey(), self.request.length, self.request.buffer)

        ack = sendWithAck(send_msg, server, self.clnt, self.request.getKey())
        if (ack == False):
            reply_dict[self.request.getKey()][0].release()
            return -1

        self.clnt.setBlocking()
        data, server = self.clnt.Read()

        dict_reply = unzip(data)
        print("==============GOTREPLY==============")
        print(data)

        self.clnt.Send("ACK_RECEIVED".encode(), server)

#        if (reply_dict[self.request.getKey()][1])
        self.reply.setData(dict_reply['buffer'])
        self.reply.setLength(dict_reply['length'])

        reply_dict[self.request.getKey()][0].release()
        """
        if(findDuplicateRec(dict_reply.get("key"),dict_reply.get("lenght"),dict_reply.get("buffer")) == False):
            print("Received relpy_%d from server %s" % (dict_reply.get("key"),server))    
            reply_buffer.append(dict_reply)
            print("sending ACK_RECIVED in server")
        else:
            print("Received duplicate relpy_%d from server %s" % (dict_reply.get("key"),server))
            clnt.Send("Ack_recieved".encode(),server)
        """

def getReply(reqid, block):
    if (block == True):
        reply_dict[reqid][0].acquire()
    else:
        if (reply_dict[reqid][0].locked()):
            return -1

    reply = reply_dict[reqid][1]

    del reply_dict[reqid]

    return reply.data, reply.length

def sendRequest(svcid,buffer,length):
    req = Request(svcid,buffer,length)
    rep = Reply()
    reply_dict[req.getKey()] = (threading.Semaphore(0), Reply())
    sendRequestThrd = sendRequestThread(req, rep)
    sendRequestThrd.start()
    return req.getKey()
