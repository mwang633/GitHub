using Microsoft.WindowsAzure.Storage.Table;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Collector
{
    public class QuoteEntity : TableEntity
    {
        public QuoteEntity(DateTime snapshotTime, string symbol, double price)
        {
            this.PartitionKey = RoundMinute(snapshotTime);
            this.RowKey = symbol;
            this.Price = price;
        }

        public QuoteEntity() { }

        public double Price { get; set; }

        public static string RoundMinute(DateTime time){
            return time.ToString("yyyyMMdd_HHmm");
        }
    }
}
