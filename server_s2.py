#!/usr/bin/env python3
# -*- coding: utf-8 -*-


import socket
import struct

class server:
    
    def __init__(self,multicast_group,server_1,server_2):
       
        self.multicast_group = multicast_group
        self.server_1 = server_1
        self.server_2 = server_2
        
        #create udp socket
        self.udp_sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
        
       #bind udp socket 
        self.udp_sock.bind(self.server_2)
        
        #create the socket
        self.sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
        
        # Bind to the server address
        self.sock.bind(self.server_1)
        
        # Tell the operating system to add the socket to the multicast group
        # on all interfaces.
        group = socket.inet_aton(self.multicast_group)
        mreq = struct.pack("=4sl", group, socket.INADDR_ANY)
        self.sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)
    
    
    def udpSend(self,message,address):
        message = message
        sent = self.udp_sock.sendto(message,address)
        return sent
    
    def udpRead(self):
        data, client = self.udp_sock.recvfrom(1024)
        #return data.decode(),client
        return data,client
    
    def multSend(self,message,address):
        message = message.encode()
        sent = self.sock.sendto(message,address)
        return sent
    
    def mulRead(self):
        data, client = self.sock.recvfrom(1024)
        return data.decode(),client
    
    def mulSockClose(self):
        self.sock.close()
        del self.sock
        
    def udpSockClose(self):
        self.udp_sock.close()
        del self.udp_sock

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
    print(key)
    x2 = data[4:8]
    length = int.from_bytes(x2,"big")
    print (length)
    x3 = data[8:8+length]
    buffer = int.from_bytes(x3,"big")
    print(buffer)
    
    return {"key":key,"lenght":length,"buffer":buffer}



def main():
   
    multicast_group = '226.1.1.1'
    server_1 = ('',10000)
    server_2 = ('',10001)
    srv = server(multicast_group,server_1,server_2)
    # Receive/respond loop
    #while True:
    print ('\nwaiting to receive message')
    data, address = srv.mulRead()
    print('received %s bytes from %s' % (len(data), address))
    print(data)
    
    print('sending acknowledgement to', address)
    srv.udpSend('server_ack'.encode(),address)
       
    data, address = srv.udpRead()
    print('received %s bytes from %s' % (len(data), address))
    print(data)
    
    print('sending acknowledgement to', address)
    srv.udpSend('Ack'.encode(),address)
    
    data, address = srv.udpRead()
    srv.udpSend("Ack".encode(),address)
    print(data)
    
    ret = unzip(data)
    
    srv.udpSend(zip_b(ret.get("key"),4,1),address)
    
    data, address = srv.udpRead()
    print('received %s bytes from %s' % (len(data), address))
    print(data)
    
    
if __name__ == "__main__":
    main()


