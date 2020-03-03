#!/usr/bin/env python3
# -*- coding: utf-8 -*-


import socket
import struct

class server:
    
    def __init__(self,multicast_group,server_ip,server_mul_port,server_udp_port):
        self.multicast_group = multicast_group
        self.server_mul_address = (server_ip,server_mul_port)
        self.server_udp_address = (server_ip,server_udp_port)
        #create udp socket
        self.udp_sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
        self.udp_sock.bind(self.server_udp_address)
        
        #create the socket
        self.sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
        
        # Bind to the server address
        self.sock.bind(self.server_mul_address)
        
        # Tell the operating system to add the socket to the multicast group
        # on all interfaces.
        group = socket.inet_aton(self.multicast_group)
        mreq = struct.pack("=4sl", group, socket.INADDR_ANY)
        self.sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)
    
    
    def udpSend(self,message,address):
        message = message.encode()
        sent = self.udp_sock.sendto(message,address)
        return sent
    
    def udpRead(self):
        data, client = self.udp_sock.recvfrom(1024)
        return data.decode(),client
    
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




def main():
   
    multicast_group = '226.1.1.1'
    server_address_ip = ''
    server_mul_port_num = 10000    
    server_udp_port_num = 10001
    
    srv = server(multicast_group,server_address_ip,server_mul_port_num,server_udp_port_num)
    # Receive/respond loop
    #while True:
    print ('\nwaiting to receive message')
    data, address = srv.mulRead()
        
    print('received %s bytes from %s' % (len(data), address))
    print(data)
    
    print('sending acknowledgement to', address)
    srv.udpSend('server_ack',address)
       
    data, address = srv.udpRead()
    print('received %s bytes from %s' % (len(data), address))
    print(data)
    
    srv.udpSockClose()
    srv.mulSockClose()
    
        
        

if __name__ == "__main__":
    main()


