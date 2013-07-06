

Basic requirements

Add a grails command (create-script) that creates a new command that mimics test-app
'grails virtual-split-test'
To do the following:
    Read config on how tests are to be split
        - []
        - Number of vms
        - Split strategy (file  size, history or run durations)
            - e.g run all test phases with each vm running an even (or near even) number of the total number of test in each phase
        - Starts some type of event listener that can collect output and final status from all vms
        - reports the overall result (passed or failed)
        - combines test reports from each split


    Run the splits on each vm (or locally)
        [grails split-test (phase:type) -spliit='1' -ofTotalSplits='4']
        - Should be able to run without above event listener i.e. stand alone

        - nice to have
            - Allow each split to run in parallel using groovy ProcessBuilder
