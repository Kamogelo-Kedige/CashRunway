package com.CashGuard.model;


import java.time.LocalDate;

/**
 * A single point-in-time warning, either the risk level changed,
 * or the balance crossed a capacity milestone.
 * Used for displaying dashboard warnings
 */
public record Warning(LocalDate date, String message) { }
