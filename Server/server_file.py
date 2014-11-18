import os
import re

class ServerFiles(object):
	re_rule = {
		'mp4':r'(\w+\.mp4$)',
		'rmvb':r'(\w+\.rmvb)',
		'avi':r'(\w+\.avi)',
		#bellow is to test
		'py':r'(\w+\.py)',
		'iml':r'(\w+\.iml)',
	}
	def __init__(self,**kargs):
		self.root = os.path.join(os.path.dirname(__file__),"static")
		if "path" in kargs:
			self.root = kargs['path']

	def getFiles(self,*types):
		"""
			args : file type  , we will found if this type in re_rule
		"""
		all_files = os.listdir(self.root)
		res = []
		for each_type in types:
			self.match_type = each_type
			res += map(self.if_match, all_files)
		res = [i for i in res if i ]
		return res

	def get_all(self):
		return os.listdir(self.root)

	def if_match(self,file_name):

		if not self.match_type:
			raise AttributeError("no type decided")
		if not self.match_type in self.re_rule:
			raise KeyError("{} is not in our re rules".format(self.match_type))

		rule = re.compile(self.re_rule[self.match_type])
		if re.match(rule, file_name):
			return file_name
		else:
			return None




if __name__ == "__main__":
	sd =ServerFiles(path="./")
	print sd.getFiles("py","iml")


