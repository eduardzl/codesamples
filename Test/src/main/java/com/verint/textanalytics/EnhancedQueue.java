package com.verint.textanalytics;

import java.util.Stack;

/**
 * Created by EZlotnik on 3/1/2017.
 */
public class EnhancedQueue<T> {
    private Stack<T> inputStack;
    private Stack<T> outputStack;

    public EnhancedQueue () {
        this.inputStack = new Stack<>();
        this.outputStack = new Stack<>();
    }

    public void enqueue (T elem) {
        if (elem != null) {
            inputStack.push(elem);
        }
    }

    public T dequeue () {
        T elem = null;

        if (this.outputStack.empty()) {
            while (!this.inputStack.isEmpty()) {
                this.outputStack.push(this.inputStack.pop());
            }
        }

        if (!this.outputStack.isEmpty()) {
            elem = this.outputStack.pop();
        }

        return elem;
    }
}
