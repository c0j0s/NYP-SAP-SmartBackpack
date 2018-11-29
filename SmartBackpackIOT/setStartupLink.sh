sudo rm -f /etc/systemd/system/SBP_*
sudo systemctl link /home/pi/SmartBackpack/SBP_Sensor_Server.service
sudo systemctl link /home/pi/SmartBackpack/SBP_BT_Server.service
echo "COMPLETED"