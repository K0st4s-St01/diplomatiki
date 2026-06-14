#!/bin/zsh

sudo systemctl stop containerd;
sudo systemctl stop docker;
sudo systemctl status containerd --no-pager;
sudo systemctl status docker --no-pager;
