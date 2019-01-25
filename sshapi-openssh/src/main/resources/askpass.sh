#!/bin/bash

# Access the pipe-in file and write to it signalling we are waiting for a password

echo 'ready-for-password' > $SSH_AUTH_PIPE_IN_FILE

# Wait for response and echo it back to terminal for SSH to read
read line < $SSH_AUTH_PIPE_OUT_FILE
echo "${line}"