#!/bin/bash -p

(
    idle=$(vmstat 60 2)
    idle=${idle##* }

    ((idle<80)) && {
       echo >&1 Trop de charge: Abandon
       exit 1
    }
