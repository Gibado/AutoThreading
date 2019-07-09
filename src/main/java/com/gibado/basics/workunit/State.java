package com.gibado.basics.workunit;

import com.gibado.basics.sharable.ISharable;

/**
 * Different states that a {@link ITaskRunner} can be in.  This is ordered by importance.
 */
public enum State {
    /** {@link ITaskRunner} has finished its task successfully */
    DONE,
    /** {@link ITaskRunner} has started performing its task */
    IN_PROGRESS,
    /** {@link ITaskRunner} has started attempting to claim its required {@link ISharable} resources */
    INITIATED,
    /** {@link ITaskRunner} is ready to perform its task */
    READY,
    /** Requires at least 1 more {@link ISharable} resource required for performing the {@link WorkUnit} task */
    WAITING_RESOURCE,
    /** Requires a dependent {@link ITaskRunner} to process before continuing */
    WAITING_DEPENDENT,
    /** {@link ITaskRunner} encountered an error */
    ERROR
}
