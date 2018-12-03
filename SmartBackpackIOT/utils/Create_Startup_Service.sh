rm -f /etc/systemd/system/SBP_*
systemctl link /home/pi/SmartBackpackIOT/utils/SBP_Sensor_Server.service
systemctl link /home/pi/SmartBackpackIOT/utils/SBP_BT_Server.service
echo "COMPLETED"