package com.example.fitapp.data.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001J\u001c\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\'J\u0016\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u0016\u0010\f\u001a\u00020\r2\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u000e\u00a8\u0006\u000f"}, d2 = {"Lcom/example/fitapp/data/db/IntakeDao;", "", "dayEntriesFlow", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/fitapp/data/db/IntakeEntryEntity;", "epochSec", "", "insert", "", "entry", "(Lcom/example/fitapp/data/db/IntakeEntryEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "totalForDay", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao()
public abstract interface IntakeDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.example.fitapp.data.db.IntakeEntryEntity entry, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "\n        SELECT COALESCE(SUM(kcal),0) FROM intake_entries\n        WHERE date(datetime(timestamp,\'unixepoch\')) = date(:epochSec,\'unixepoch\',\'localtime\')\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object totalForDay(long epochSec, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Query(value = "\n        SELECT * FROM intake_entries\n        WHERE date(datetime(timestamp,\'unixepoch\')) = date(:epochSec,\'unixepoch\',\'localtime\')\n        ORDER BY timestamp DESC\n    ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.fitapp.data.db.IntakeEntryEntity>> dayEntriesFlow(long epochSec);
}