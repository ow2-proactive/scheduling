SET IT=%PA_TASK_ITERATION%
SET DUP=%PA_TASK_REPLICATION%

echo param it %2:param dup %3:env it %PA_TASK_ITERATION%:env dup %PA_TASK_REPLICATION% > %1native_result_%IT%_%DUP%

exit 0