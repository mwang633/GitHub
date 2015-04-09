using Collector;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web;
using System.Web.Http;

namespace SP500Web
{
    public class DataController : ApiController
    {
        // POST api/<controller>
        // public List<TableRow> Post([FromBody]int lookbackMins)
        public List<TableRow> Post([FromUri]int lookbackMins)
        {
            MvcApplication application = HttpContext.Current.ApplicationInstance as MvcApplication;

            var prevSnapshot = application
                                .GetTable()
                                .CreateQuery<QuoteEntity>()
                                .Where(e => e.PartitionKey == QuoteEntity.RoundMinute(DateTime.UtcNow.AddMinutes(-1.0 - lookbackMins)))
                                .ToList();

            var currSnapshot = application
                                .GetTable()
                                .CreateQuery<QuoteEntity>()
                                .Where(e => e.PartitionKey == QuoteEntity.RoundMinute(DateTime.UtcNow.AddMinutes(-1.0)))
                                .ToList();

            var joined = prevSnapshot.Join(
                currSnapshot,
                e => e.RowKey,
                e => e.RowKey,
                (p, c) => new TableRow()
                            {
                                symbol = p.RowKey,
                                time1 = p.PartitionKey,
                                time2 = c.PartitionKey,
                                price1 = p.Price,
                                price2 = c.Price,
                                changePerc = 100.0 * (c.Price - p.Price) / p.Price
                            })
                .OrderByDescending(r => Math.Abs(r.changePerc))
                .ToList();

            return joined;
        }

        public class TableRow
        {
            public String symbol { get; set; }
            public String time1 { get; set; }
            public Double price1 { get; set; }
            public String time2 { get; set; }
            public Double price2 { get; set; }
            public Double changePerc { get; set; }
        }
    }
}