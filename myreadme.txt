Adding the transfer functionality to the Accounts rest endpoint.
1) Allows the transfer of amount between two accounts in multithreaded environment
2) Dealock free
3) checks the basic validation on trnasaction amount as well as no overdraft on from account


If given more time,
 1) would try to accompolish the transaction behaviour using spring transaction manager on accounts transfer.
 2) To increase the throughput, non blocking IO implementation could be implemented such that the incoming request thread could not be blocked
    due to  locking on synchronized account transfer implementation.

