#!/bin/sh

echo User var 1 is $USER_VAR_1
if [[ $USER_VAR_1 != ".User Value 1." ]]
then
exit 1;
fi
echo User var 2 is $USER_VAR_2
if [[ $USER_VAR_2 != ".User Value 2." ]]
then
exit 1;
fi

echo TID $PAS_TASK_ID
echo TIN $PAS_TASK_NAME
echo JID $PAS_JOB_ID
echo JIN $PAS_JOB_NAME

exit 0;

