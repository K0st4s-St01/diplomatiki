#!/bin/zsh

sudo systemctl start containerd;
sudo systemctl start docker;
sudo systemctl status containerd --no-pager;
sudo systemctl status docker --no-pager;
