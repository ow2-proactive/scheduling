importPackage(java.io);
print("clean working directory \n");
for(i=0; i<args.length;i++)
{
	var f= new File(args[i]);
	if(f["delete"]()) {
		print(args[i] +" deleted\n");
	} else {
		print("deleting "+ args[i] +" failed\n");
        }
}
