import tornado
import tornado.ioloop
import tornado.web
from tornado import gen
from tornado.ioloop import IOLoop
from tornado import websocket
import motor 
import json
import os

from server_file import ServerFiles

db_client = motor.MotorClient()

setting = {
        'debug' : True,
        'autoreload' : True,
        'db' : db_client,
        'static_path':os.path.join(os.path.dirname(__file__),"static"),
        }

class JsonTools:
    @staticmethod
    def tojson(data):
        return json.dumps(data)

    @staticmethod
    def toDict(string):
	try:
        	return json.loads(string)
	except ValueError:
		print string 
	raise ValueError("can not be decode as json")
    @staticmethod
    def respondJson(res):
        return json.dumps({'des':'respond','res':res })

class BaseWebSockerHandler(websocket.WebSocketHandler):
    connections = {}
    devices = {}
    def prepare(self):
        self.db = self.settings['db']
        self.static_path = self.settings['static_path']
    def check_if_onlion(self,ip):
        if ip in connections:
            return True
        return False

    @gen.coroutine
    def save_file(self,file_obj):
        date = file_obj['data'].decode('utf8')
        file_len = file_obj['len']
        file_name = file_obj['name']
        real_len = len(data)
        if real_len == file_len:
            with open(uri,'wb') as handler:
                handler.write(data)
            self.write_message(JsonTools.respondJson('ok'))
        else:
            self.write_message(JsonTools.respondJson('retry'))
    def write_message(self,*args,**kargs):
	print args
	print kargs
	super(BaseWebSockerHandler,self).write_message(*args,**kargs)		

    def open(self):
        BaseWebSockerHandler.connections[self.request.remote_ip] = 'unknow'
        print "WebSocket opened\n"
        print "static {}".format(self.static_path)

	
   
    @tornado.web.asynchronous
    def on_message(self,message):
	print "primary message {}".format(message)
        root_obj = JsonTools.toDict(message)
        if root_obj['des'] == "register":
            if root_obj['device']['type'] == 'device':
                self.login(root_obj['device']['id'])
                self.write_message(JsonTools.respondJson('ok'))
            elif root_obj['device']['type'] == 'controller':
                self.write_message(JsonTools.tojson({
                        'devices':BaseWebSockerHandler.devices,
                    }))
        elif root_obj['des'] == "update_file":
            file_obj = JsonTools.toDict(message)
            save_file(file_obj)
        elif root_obj['des'] == "bash":
            command = root_obj['command']
            res = os.popen(command).read()
            self.write_message(JsonTools.tojson({
                    'des':'res',
                    'content':res,
                }))
        elif root_obj['des'] == 'show_file':
            files_handler = ServerFiles()
            files = files_handler.get_all()
            self.write_message(JsonTools.tojson({
                    'des':'res',
                    'content':files,
                }))
        elif root_obj['des'] == 'send_file':
            target = root_obj['target']
            if not self.check_if_onlion(target):
                self.write_message(JsonTools.respondJson('no'))
            connection = self.get_device(target)
            connection.write_message(JsonTools.tojson({
                    'des':'download',
                    'name':root_obj['name'],
                    'url':os.path.join(self.static_path,root_obj['name']),
                }))
            self.write_message(JsonTools.respondJson('ok'))
        elif root_obj['des'] == 'command':
            print (root_obj)
            target = root_obj['target']
            
            if not self.check_if_onlion(target):
                self.write_message(JsonTools.respondJson('no'))
            connection = self.get_device(target)

            connection.write_message(JsonTools.tojson(root_obj['command'])
            
            self.write_message(JsonTools.respondJson('ok'))

    def on_close(self):
        self.logout() 
    
    def send_file(self,file_name):
        self.write_message(JsonTools.tojson({
                'des':'download',
                'name':file_name,
                'url':os.path.join(self.static_path,file_name),

            }))

    def get_device(self,device_id):
        ip = BaseWebSockerHandler.devices[device_id]
        return BaseWebSockerHandler.connections[ip]

    def login(self,device_id):
        BaseWebSockerHandler.connections[self.request.remote_ip] = device_id
        BaseWebSockerHandler.devices[device_id] = self
        print BaseWebSockerHandler.connections
        print BaseWebSockerHandler.devices

    def logout(self):
        device_id = BaseWebSockerHandler.connections[self.request.remote_ip]
        BaseWebSockerHandler.devices.pop(device_id)
        BaseWebSockerHandler.connections.pop(self.request.remote_ip)
        print "{}'s device {} : lose connecting ".format(self.request.remote_ip,device_id)

application = tornado.web.Application([
        (r'/websocket',BaseWebSockerHandler),
    ],**setting)


if __name__ == "__main__":
    application.listen(9180)
    tornado.ioloop,IOLoop.instance().start()
