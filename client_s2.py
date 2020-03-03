#!/usr/bin/env python3
# -*- coding: utf-8 -*-



import socket
import struct


class client:
    
    def __init__(self,multicast_ip,port_num,client_ip,client_udp_port):
        self.multicast_group = (multicast_ip,port_num)
        
        #create  udp socket
        self.udp_sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
        
        #bind udp socket
        self.udp_sock.bind((client_ip,client_udp_port))
        
        #create the datagram socket
        self.sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
         
        #set the ttl to 1  
        self.ttl = struct.pack('b',1)
        self.sock.setsockopt(socket.IPPROTO_IP,socket.IP_MULTICAST_TTL,self.ttl)
    
    def Send(self,message,address):
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
    



def main():
    message = 'RR_CLIENT_44'
    multicast_group_ip = '226.1.1.1'
    multicast_port_num = 10000
    client_ip = ''
    client_port_num = 10002
    clnt = client(multicast_group_ip,multicast_port_num,client_ip,client_port_num)
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
    
    message = bytearray("client_ack",'ascii')
    
    clnt.Send(message,server)
    
    
        
    
    print('closing socket')
    clnt.mulsockClose()
    clnt.udpsockClose()
        
        

if __name__ == "__main__":
    main()



    
    
