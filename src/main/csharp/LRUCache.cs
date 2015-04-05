using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LRUCache
{
        using System;
        using System.Collections.Generic;
        using System.Diagnostics;
        using System.Threading;

    public class LruCache<TKey, TValue> : IDictionary<TKey, TValue>
    {
        private Dictionary<TKey, LinkedListNode<KeyValuePair<TKey, TValue>>> cacheDictionary = new Dictionary<TKey, LinkedListNode<KeyValuePair<TKey, TValue>>>();
        private LinkedList<KeyValuePair<TKey, TValue>> lruLinkedList = new LinkedList<KeyValuePair<TKey, TValue>>();

        private readonly int maxSize;

        public LruCache(int maxCacheSize = 1000)
        {
            if (maxCacheSize <= 1)
            {
                throw new ArgumentException("maxCacheSize must be greater than 1");
            }

            this.maxSize = maxCacheSize;
        }

        public void Add(TKey key, TValue value)
        {
            this.Add(new KeyValuePair<TKey, TValue>(key, value));
        }

        public void Add(KeyValuePair<TKey, TValue> item)
        {
            lruLinkedList.AddFirst(item);
            this.cacheDictionary.Add(item.Key, lruLinkedList.First);

            if (lruLinkedList.Count > this.maxSize)
            {
                LinkedListNode<KeyValuePair<TKey, TValue>> leastVisited = lruLinkedList.Last;
                lruLinkedList.RemoveLast();
                cacheDictionary.Remove(leastVisited.Value.Key);
            }
        }

        public bool TryGetValue(TKey key, out TValue value)
        {
            LinkedListNode<KeyValuePair<TKey, TValue>> node;
            if (!this.cacheDictionary.TryGetValue(key, out node))
            {
                value = default(TValue);
                return false;
            }
            else
            {
                // Promotes node to First if not already
                if (node != this.lruLinkedList.First)
                {
                    this.lruLinkedList.Remove(node);
                    this.lruLinkedList.AddFirst(node);
                }

                value = node.Value.Value;
                return true;
            }
        }

        public TValue this[TKey key]
        {
            get
            {
                TValue value;
                this.TryGetValue(key, out value);
                return value;
            }
            set
            {
                LinkedListNode<KeyValuePair<TKey, TValue>> node;

                if (this.cacheDictionary.TryGetValue(key, out node))
                {
                    this.lruLinkedList.Remove(node);
                    this.cacheDictionary.Remove(key);
                    this.Add(key, value);
                }
                else
                {
                    this.Add(key, value);
                }
            }
        }

        public void Clear()
        {
            this.lruLinkedList.Clear();
            this.cacheDictionary.Clear();
        }

        public bool Contains(KeyValuePair<TKey, TValue> item)
        {
            return this.cacheDictionary.ContainsKey(item.Key);
        }

        public int Count
        {
            get { return this.lruLinkedList.Count; }
        }

        public bool IsReadOnly
        {
            get { return false; }
        }

        public bool ContainsKey(TKey key)
        {
            return this.cacheDictionary.ContainsKey(key);
        }

        public ICollection<TKey> Keys
        {
            get { return this.cacheDictionary.Keys; }
        }

        public bool Remove(TKey key)
        {
            LinkedListNode<KeyValuePair<TKey, TValue>> node;

            if (this.cacheDictionary.TryGetValue(key, out node))
            {
                this.cacheDictionary.Remove(key);
                this.lruLinkedList.Remove(node);
                return true;
            }
            else
            {
                return false;
            }
        }

        public bool Remove(KeyValuePair<TKey, TValue> item)
        {
            return this.Remove(item.Key);
        }

        public IEnumerator<KeyValuePair<TKey, TValue>> GetEnumerator()
        {
            return this.lruLinkedList.GetEnumerator();
        }

        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        {
            return this.lruLinkedList.GetEnumerator();
        }

        public ICollection<TValue> Values
        {
            get
            {
                throw new NotSupportedException();
            }
        }

        public void CopyTo(KeyValuePair<TKey, TValue>[] array, int arrayIndex)
        {
            throw new NotSupportedException();
        }
    }
}
