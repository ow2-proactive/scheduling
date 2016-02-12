Thread nonstopping = new Thread(new Runnable() {
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // ignore interrupt
            }
        }
    }
});

nonstopping.setDaemon(false);
nonstopping.start();
result = true;