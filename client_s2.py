#!/usr/bin/env python3
# -*- coding: utf-8 -*-



import socket
import struct
import random


class client:
    
    def __init__(self,multicast_group,client):
        self.multicast_group = multicast_group
        self.client = client
         
        #create the datagram socket
        self.sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
        #set the ttl to 1  
        self.ttl = struct.pack('b',1)
        self.sock.setsockopt(socket.IPPROTO_IP,socket.IP_MULTICAST_TTL,self.ttl)
       
        #create  udp socket
        self.udp_sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
        
        #bind udp socket
        self.udp_sock.bind(client)
    
    def Send(self,message,address):
        message = message.encode()
        sent = self.udp_sock.sendto(message, address)
        return sent
    
    def Read(self):
        data, server = self.udp_sock.recvfrom(1024)
        return data.decode(),server
    
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
    
class Request:
    def __init__(self,svcid,buffer,length):
        self.svcid = svcid
        self.buffer = buffer
        self.length = length
        self.key =  random.randint(1,1000000000)
        
    def asDict(self):
        return{'key':self.key, 'svcid':self.svcid, 'buffer':self.buffer, 'len':self.length }
        
    
        
    


def main():
    message = 'RR_CLIENT_44'
    multicast_group = ('226.1.1.1',10000);
    clien_t = ('',10002)
    clnt = client(multicast_group,clien_t)
    #while True:
        # Send data to the multicast group
    sent = clnt.multiSend(message)
    print("waiting to receive")
        # Look for responses from all recipients
    try:
        data, server = clnt.multiRead()
    except socket.timeout:
        print('timed out, no more responses')
    else:
        print('received "%s" from %s' % (data, server))
        
    clnt.Send("client_ack",server)
    
    data, server = clnt.Read()
    print('received "%s" from %s' % (data, server))
    
        
    
    print('closing socket')
    clnt.mulsockClose()
    clnt.udpsockClose()
        
        

if __name__ == "__main__":
    main()



    
    