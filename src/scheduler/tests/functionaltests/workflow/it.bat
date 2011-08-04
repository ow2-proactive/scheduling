SET IT=%PAS_TASK_ITERATION%
SET DUP=%PAS_TASK_REPLICATION%

echo param it %2:param dup %3:env it %PAS_TASK_ITERATION%:env dup %PAS_TASK_REPLICATION% > %1native_result_%IT%_%DUP%

exit 0