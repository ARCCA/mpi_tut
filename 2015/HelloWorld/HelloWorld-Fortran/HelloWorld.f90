program hello

  include 'mpif.h'

  integer mpierr, rank, procs

  call MPI_Init ( mpierr )
  call MPI_Comm_size ( MPI_COMM_WORLD , procs , mpierr )
  call MPI_Comm_rank ( MPI_COMM_WORLD , rank , mpierr )

  write (*,*) 'Hello world from ', rank, 'of', procs 

  call MPI_Finalize ( mpierr )

end program hello
