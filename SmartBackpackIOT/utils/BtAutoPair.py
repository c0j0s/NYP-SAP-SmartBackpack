#!/usr/bin/python3
# encoding=utf8

import sys
import time
import pexpect
import subprocess

class BtAutoPair:
	"""Class to auto pair and trust with bluetooth."""

	def __init__(self):
		p = subprocess.Popen("/usr/local/bin/auto-agent", shell = False)
		out = subprocess.check_output("/usr/sbin/rfkill unblock bluetooth", shell = True)
		self.child = pexpect.spawn("bluetoothctl", echo = False)

	def get_output(self,command, pause = 0):
		"""Run a command in bluetoothctl prompt, return output as a list of lines."""
		self.child.send(command + "\n")
		time.sleep(pause)
		start_failed = self.child.expect(["bluetooth", pexpect.EOF])

		if start_failed:
			raise Exception("Bluetoothctl failed after running " + command)
			
		return self.child.before.split("\r\n")

	def enable_pairing(self):
		"""Make device visible to scanning and enable pairing."""
		print("pairing enabled")
		try:
			out = self.get_output("power on")
			out = self.get_output("discoverable on")
			out = self.get_output("pairable on")
			out = self.get_output("agent off")

		except Exception as e:
			print(e)
			return None

	def disable_pairing(self):
		"""Disable devices visibility and ability to pair."""
		try:
			out = self.get_output("discoverable off")
			out = self.get_output("pairable off")

		except Exception as e:
			print(e)
			return None