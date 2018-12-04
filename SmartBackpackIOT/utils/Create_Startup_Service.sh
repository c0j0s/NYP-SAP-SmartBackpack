rm -f /etc/systemd/system/SBP_*
sudo systemctl link /home/pi/SmartBackpackIOT/utils/SBP_Sensor_Server.service
sudo systemctl link /home/pi/SmartBackpackIOT/utils/SBP_BT_Server.service
sudo systemctl link /home/pi/SmartBackpackIOT/utils/SBP_Service_Monitor.service
echo "COMPLETED"