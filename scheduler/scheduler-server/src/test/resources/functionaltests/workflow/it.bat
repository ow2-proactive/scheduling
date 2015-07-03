SET IT=%variables_PA_TASK_ITERATION%
SET DUP=%variables_PA_TASK_REPLICATION%

echo param it %2:param dup %3:env it %variables_PA_TASK_ITERATION%:env dup %variables_PA_TASK_REPLICATION% > %1native_result_%IT%_%DUP%

exit 0